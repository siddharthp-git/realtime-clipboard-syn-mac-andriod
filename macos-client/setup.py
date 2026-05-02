from setuptools import setup

APP = ['listner.py']
DATA_FILES = ['serviceaccount.json']
OPTIONS = {
    'argv_emulation': True,
    'plist': {
        'LSUIElement': True,  # This makes it a background app (no dock icon)
        'CFBundleName': 'ClipboardSync',
        'CFBundleDisplayName': 'Clipboard Sync',
        'CFBundleIdentifier': 'com.siddharth.clipboardsync',
        'CFBundleVersion': '1.0.0',
        'CFBundleShortVersionString': '1.0.0',
    },
    'packages': ['pyperclip', 'firebase_admin'],
}

setup(
    app=APP,
    data_files=DATA_FILES,
    options={'py2app': OPTIONS},
    setup_requires=['py2app'],
)
