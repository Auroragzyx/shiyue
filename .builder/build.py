import os
import sys
import shutil
import zipfile
import subprocess
import urllib.request
import traceback

BUILDER_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIR = os.path.dirname(BUILDER_DIR)
ANDROID_DIR = os.path.join(PROJECT_DIR, "android", "src", "main")
OUT_DIR = os.path.join(BUILDER_DIR, "out")

TOOLS = {
    "jre": {
        "url": "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.12%2B7/OpenJDK17U-jre_x64_windows_hotspot_17.0.12_7.zip",
        "file": os.path.join(BUILDER_DIR, "jre.zip"),
        "extract_check": os.path.join(BUILDER_DIR, "jre", "bin", "java.exe")
    },
    "ecj": {
        "url": "https://repo1.maven.org/maven2/org/eclipse/jdt/ecj/3.38.0/ecj-3.38.0.jar",
        "file": os.path.join(BUILDER_DIR, "ecj.jar")
    },
    "android_jar": {
        "url": "https://raw.githubusercontent.com/Sable/android-platforms/master/android-30/android.jar",
        "file": os.path.join(BUILDER_DIR, "android.jar")
    },
    "aapt2_jar": {
        "url": "https://dl.google.com/dl/android/maven2/com/android/tools/build/aapt2/8.5.0-11315950/aapt2-8.5.0-11315950-windows.jar",
        "file": os.path.join(BUILDER_DIR, "aapt2.jar"),
        "extract_check": os.path.join(BUILDER_DIR, "aapt2.exe")
    },
    "r8": {
        "url": "https://dl.google.com/dl/android/maven2/com/android/tools/r8/8.5.35/r8-8.5.35.jar",
        "file": os.path.join(BUILDER_DIR, "r8.jar")
    },
    "signer": {
        "url": "https://github.com/patrickfav/uber-apk-signer/releases/download/v1.3.0/uber-apk-signer-1.3.0.jar",
        "file": os.path.join(BUILDER_DIR, "uber-apk-signer.jar")
    }
}

def download_file(url, target_path, name):
    if os.path.exists(target_path) and os.path.getsize(target_path) > 1000:
        print(f"[OK] {name} already exists.")
        return
    print(f"[DOWNLOAD] Downloading {name}...")
    headers = {'User-Agent': 'Mozilla/5.0'}
    req = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(req, timeout=60) as resp, open(target_path, 'wb') as f:
        total = int(resp.headers.get('Content-Length', 0))
        downloaded = 0
        while True:
            chunk = resp.read(1024 * 512)
            if not chunk:
                break
            f.write(chunk)
            downloaded += len(chunk)
            if total > 0:
                percent = int(downloaded / total * 100)
                sys.stdout.write(f"\r  Progress: {percent}% ({downloaded // (1024*1024)}MB / {total // (1024*1024)}MB)")
                sys.stdout.flush()
    print(f"\n[OK] {name} downloaded successfully.")

def setup_toolchain():
    print("=== Step 1: Preparing Micro Android Toolchain ===")
    
    # 1. Download jars & zips
    for key, info in TOOLS.items():
        download_file(info["url"], info["file"], key)

    # 2. Extract JRE
    java_exe = TOOLS["jre"]["extract_check"]
    if not os.path.exists(java_exe):
        print("[EXTRACT] Extracting JRE...")
        with zipfile.ZipFile(TOOLS["jre"]["file"], 'r') as z:
            # The root dir inside zip might be jdk-17.0.12+7-jre
            members = z.namelist()
            root_dir = members[0].split('/')[0]
            z.extractall(BUILDER_DIR)
            extracted_root = os.path.join(BUILDER_DIR, root_dir)
            target_jre = os.path.join(BUILDER_DIR, "jre")
            if os.path.exists(target_jre):
                shutil.rmtree(target_jre)
            os.rename(extracted_root, target_jre)
        print("[OK] JRE extracted.")

    # 3. Extract aapt2.exe from jar
    aapt2_exe = TOOLS["aapt2_jar"]["extract_check"]
    if not os.path.exists(aapt2_exe):
        print("[EXTRACT] Extracting aapt2.exe...")
        with zipfile.ZipFile(TOOLS["aapt2_jar"]["file"], 'r') as z:
            with z.open("aapt2.exe") as src, open(aapt2_exe, "wb") as dst:
                dst.write(src.read())
        print("[OK] aapt2.exe extracted.")

    return java_exe, aapt2_exe

def build_apk(java_exe, aapt2_exe):
    print("=== Step 2: Compiling Swiss Days Android App ===")
    if os.path.exists(OUT_DIR):
        shutil.rmtree(OUT_DIR)
    os.makedirs(OUT_DIR, exist_ok=True)

    classes_dir = os.path.join(OUT_DIR, "classes")
    os.makedirs(classes_dir, exist_ok=True)

    # Copy latest index.html to assets
    assets_dir = os.path.join(ANDROID_DIR, "assets")
    os.makedirs(assets_dir, exist_ok=True)
    shutil.copy2(os.path.join(PROJECT_DIR, "index.html"), os.path.join(assets_dir, "index.html"))

    # 1. Compile Java using ECJ
    print("-> Compiling Java code with ECJ...")
    java_src_dir = os.path.join(ANDROID_DIR, "java", "com", "shiyue", "app")
    java_files = [os.path.join(java_src_dir, f) for f in os.listdir(java_src_dir) if f.endswith(".java")]
    cmd_ecj = [
        java_exe, "-jar", TOOLS["ecj"]["file"],
        "-7",
        "-cp", TOOLS["android_jar"]["file"],
        "-d", classes_dir
    ] + java_files
    res = subprocess.run(cmd_ecj, capture_output=True, text=True)
    if res.returncode != 0:
        print("[ERROR] Java compilation failed:", res.stderr)
        return False
    print("   [OK] Java classes compiled.")

    # 2. Dex classes with D8 (inside R8)
    print("-> Converting to Android DEX with D8...")
    class_files = []
    for root, _, files in os.walk(classes_dir):
        for f in files:
            if f.endswith(".class"):
                class_files.append(os.path.join(root, f))
    
    cmd_d8 = [
        java_exe, "-cp", TOOLS["r8"]["file"], "com.android.tools.r8.D8",
        "--min-api", "21",
        "--lib", TOOLS["android_jar"]["file"],
        "--output", OUT_DIR
    ] + class_files
    res = subprocess.run(cmd_d8, capture_output=True, text=True)
    if res.returncode != 0:
        print("[ERROR] D8 dexing failed:", res.stderr)
        return False
    print("   [OK] classes.dex generated.")

    # 3. Compile resources with aapt2
    print("-> Compiling Android resources with aapt2...")
    res_dir = os.path.join(ANDROID_DIR, "res")
    compiled_res = os.path.join(OUT_DIR, "res.zip")
    cmd_aapt_compile = [
        aapt2_exe, "compile", "--dir", res_dir, "-o", compiled_res
    ]
    res = subprocess.run(cmd_aapt_compile, capture_output=True, text=True)
    if res.returncode != 0:
        print("[ERROR] aapt2 compile failed:", res.stderr)
        return False

    # 4. Link resources & assets into unaligned APK
    print("-> Linking APK with aapt2...")
    manifest = os.path.join(ANDROID_DIR, "AndroidManifest.xml")
    unaligned_apk = os.path.join(OUT_DIR, "unaligned.apk")
    cmd_aapt_link = [
        aapt2_exe, "link",
        "-I", TOOLS["android_jar"]["file"],
        "--manifest", manifest,
        "-A", assets_dir,
        "-o", unaligned_apk,
        compiled_res
    ]
    res = subprocess.run(cmd_aapt_link, capture_output=True, text=True)
    if res.returncode != 0:
        print("[ERROR] aapt2 link failed:", res.stderr)
        return False
    print("   [OK] Base APK created.")

    # 5. Inject classes.dex into unaligned.apk
    print("-> Injecting classes.dex into APK...")
    dex_path = os.path.join(OUT_DIR, "classes.dex")
    with zipfile.ZipFile(unaligned_apk, 'a') as apk_zip:
        apk_zip.write(dex_path, "classes.dex")
    print("   [OK] classes.dex injected.")

    # 6. Zipalign and sign with uber-apk-signer using Custom Release Keystore
    print("-> Zipaligning and signing APK with Custom Release Keystore...")
    signed_dir = os.path.join(OUT_DIR, "signed")
    if os.path.exists(signed_dir):
        shutil.rmtree(signed_dir)
    os.makedirs(signed_dir, exist_ok=True)
    
    keystore_path = os.path.join(BUILDER_DIR, "shiyue.jks")
    cmd_sign = [
        java_exe, "-jar", TOOLS["signer"]["file"],
        "--apks", unaligned_apk,
        "--out", signed_dir,
        "--ks", keystore_path,
        "--ksAlias", "shiyue",
        "--ksPass", "shiyue888888",
        "--ksKeyPass", "shiyue888888"
    ]
    res = subprocess.run(cmd_sign, capture_output=True, text=True)
    if res.returncode != 0:
        print("[ERROR] uber-apk-signer failed:", res.stderr)
        return False
    print("   [OK] APK signed successfully with Release Keystore.")

    # Locate generated signed APK
    signed_apk = None
    for f in os.listdir(signed_dir):
        if f.endswith(".apk"):
            signed_apk = os.path.join(signed_dir, f)
            break
    
    if not signed_apk:
        print("[ERROR] Signed APK not found.")
        return False

    final_apk_path = os.path.join(PROJECT_DIR, "SwissDays.apk")
    shutil.copy2(signed_apk, final_apk_path)
    
    # Copy to Desktop as 时约.apk
    desktop = os.path.join(os.path.expanduser("~"), "Desktop")
    if os.path.exists(desktop):
        dest_apk = os.path.join(desktop, "时约.apk")
        shutil.copy2(signed_apk, dest_apk)
        # Clean up old named apk if exists
        old_apk = os.path.join(desktop, "SwissDays_时光刻度.apk")
        if os.path.exists(old_apk):
            try:
                os.remove(old_apk)
            except Exception:
                pass
        print(f"[SUCCESS] APK copied to Desktop: {dest_apk}")

    print(f"\n=======================================================")
    print(f"🎉 BUILD SUCCESSFUL!")
    print(f"Final APK: {final_apk_path} ({os.path.getsize(final_apk_path) // 1024} KB)")
    print(f"=======================================================\n")
    return True

if __name__ == "__main__":
    try:
        java_exe, aapt2_exe = setup_toolchain()
        ok = build_apk(java_exe, aapt2_exe)
        if not ok:
            sys.exit(1)
    except Exception as e:
        print(f"\n[FATAL ERROR] {e}")
        traceback.print_exc()
        sys.exit(1)
