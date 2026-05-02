import pyperclip
import time
import firebase_admin
from firebase_admin import credentials, db

# 🔑 Init Firebase
cred = credentials.Certificate("serviceaccount.json")
firebase_admin.initialize_app(cred, {
    "databaseURL": "https://realtime-clipboard-d3755-default-rtdb.firebaseio.com/"
})

ref = db.reference("clipboard")

device_id = "mac_1"
last_text = ""
last_timestamp = 0
ignore_next = False
print("🚀 Mac clipboard sync started...")
# 🔁 Listen for Firebase updates (incoming from Android)
def firebase_listener(event):
    global ignore_next, last_timestamp

    data = event.data
    if not data:
        return

    content = data.get("content")
    sender = data.get("device_id")
    timestamp = data.get("timestamp")

    # Ignore if same device or old update
    if sender == device_id or timestamp == last_timestamp:
        return

    print("⬇️ Received from Firebase:", content)

    ignore_next = True
    pyperclip.copy(content)
    last_timestamp = timestamp


# Attach listener
ref.listen(firebase_listener)

# 🔁 Monitor local clipboard (Mac → Firebase)
while True:
    try:
        current_text = pyperclip.paste()

        if ignore_next:
            ignore_next = False
            last_text = current_text
            continue

        if current_text != last_text:
            print("⬆️ Sending to Firebase:", current_text)

            timestamp = int(time.time())

            ref.set({
                "content": current_text,
                "device_id": device_id,
                "timestamp": timestamp
            })

            last_text = current_text
            last_timestamp = timestamp

        time.sleep(0.5)

    except KeyboardInterrupt:
        print("\nStopped.")
        break