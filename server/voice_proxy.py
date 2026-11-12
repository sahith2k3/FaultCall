"""
FaultCall — Secure ElevenLabs Voice Proxy Server
Keeps API credentials secure on the backend while streaming synthesized umpire audio
to the FaultCall Android application.
"""

import os
import requests
from flask import Flask, request, Response, jsonify

app = Flask(__name__)

# Set your ElevenLabs API Key in environment or replace directly here for local testing:
# set ELEVENLABS_API_KEY=your_key_here
ELEVENLABS_API_KEY = os.environ.get("ELEVENLABS_API_KEY", "")

# Voice ID for "The Authority" generated via Voice Design prompt
DEFAULT_VOICE_ID = os.environ.get("FAULTCALL_VOICE_ID", "21m00Tcm4TlvDq8ikWAM") # Example voice ID

@app.route("/api/voice", methods=["POST"])
def generate_voice():
    if not ELEVENLABS_API_KEY:
        return jsonify({"error": "ELEVENLABS_API_KEY not configured on proxy server"}), 500

    data = request.get_json(force=True)
    text = data.get("text", "")
    voice_id = data.get("voice_id", DEFAULT_VOICE_ID)
    category = data.get("category", "SCORE")

    if not text:
        return jsonify({"error": "Missing 'text' field"}), 400

    # Adjust model parameters based on announcement type:
    # Short calls (FAULT, LET) benefit from lower stability for projected force;
    # Score announcements benefit from higher stability for measured cadence.
    stability = 0.50 if category in ["FAULT", "DOUBLE_FAULT", "LET"] else 0.70
    similarity = 0.85

    url = f"https://api.elevenlabs.io/v1/text-to-speech/{voice_id}"
    headers = {
        "xi-api-key": ELEVENLABS_API_KEY,
        "Content-Type": "application/json",
        "Accept": "audio/mpeg"
    }

    payload = {
        "text": text,
        "model_id": "eleven_monolingual_v1",
        "voice_settings": {
            "stability": stability,
            "similarity_boost": similarity,
            "style": 0.20,
            "use_speaker_boost": True
        }
    }

    try:
        response = requests.post(url, json=payload, headers=headers, timeout=15)
        if response.status_code == 200:
            return Response(response.content, mimetype="audio/mpeg")
        else:
            return jsonify({
                "error": "ElevenLabs API error",
                "status_code": response.status_code,
                "details": response.text
            }), response.status_code
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/api/health", methods=["GET"])
def health():
    return jsonify({
        "status": "healthy",
        "service": "FaultCall Voice Proxy",
        "api_key_configured": bool(ELEVENLABS_API_KEY)
    })

if __name__ == "__main__":
    print("FaultCall Voice Proxy running on port 8080...")
    print("Configure this URL in FaultCall phone app: http://<YOUR_IP>:8080/api/voice")
    app.run(host="0.0.0.0", port=8080)
