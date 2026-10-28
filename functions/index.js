const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.onInquiryCreated = functions.firestore
  .document("studios/{studioSlug}/inquiries/{inquiryId}")
  .onCreate(async (snap, context) => {
    const data = snap.data();
    if (!data) return;

    const studioSlug = context.params.studioSlug;
    const clientName = (data.clientName || "નવો ક્લાયન્ટ").trim();
    const clientPhone = (data.clientPhone || "").trim();
    const dates = (data.dates || "બુકિંગ પૂછપરછ").trim();
    const location = (data.location || "").trim();
    const notes = (data.notes || "").trim();
    const eventType = (data.eventType || "Online Inquiry").trim();

    const title = `🔔 નવી પૂછપરછ: ${clientName}`;
    const body = `📅 ${dates} | 📞 ${clientPhone}${location ? " | 📍 " + location : ""}`;

    const topic = `studio_${studioSlug.toLowerCase().replace(/[^a-z0-9]/g, "")}`;

    const message = {
      topic: topic,
      notification: {
        title: title,
        body: body
      },
      data: {
        clientName: String(clientName),
        clientPhone: String(clientPhone),
        dates: String(dates),
        location: String(location),
        notes: String(notes),
        eventType: String(eventType),
        inquiryId: String(context.params.inquiryId)
      },
      android: {
        priority: "high",
        notification: {
          channelId: "maruti_booking_alerts_v8",
          sound: "default",
          defaultSound: true,
          defaultVibrateTimings: true,
          priority: "max",
          visibility: "public"
        }
      }
    };

    try {
      const response = await admin.messaging().send(message);
      console.log(`Successfully sent FCM message to topic ${topic}:`, response);
    } catch (error) {
      console.error(`Error sending FCM message to topic ${topic}:`, error);
    }
  });
