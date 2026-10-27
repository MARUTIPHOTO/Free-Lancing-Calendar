const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * Automatically triggers whenever a new inquiry is submitted into /studios/{studioSlug}/inquiries/{inquiryId}
 * Sends a high-priority FCM Push Notification to both the photographer's FCM Topic ("studio_{studioSlug}")
 * and directly to their device registration token stored at /studios/{studioSlug}/profile/fcmToken.
 */
exports.onInquiryCreated = onDocumentCreated(
  {
    document: "studios/{studioSlug}/inquiries/{inquiryId}",
    database: "(default)"
  },
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      console.log("No data associated with the event");
      return;
    }

    const studioSlug = event.params.studioSlug;
    const data = snapshot.data();

    const clientName = (data.clientName || "નવો ક્લાયન્ટ").trim();
    const clientPhone = (data.clientPhone || "").trim();
    const dates = (data.dates || "સામાન્ય પૂછપરછ").trim();
    const location = (data.location || "").trim();
    const notes = (data.notes || "").trim();
    const eventType = (data.eventType || "શૂટ બુકિંગ").trim();

    console.log(`New inquiry for studio: ${studioSlug} from ${clientName} (${clientPhone})`);

    const title = `🔔 નવી પૂછપરછ: ${clientName}`;
    const body = `📅 ${dates} | 📞 ${clientPhone}${location ? " | 📍 " + location : ""}`;

    const payloadData = {
      clientName: clientName,
      clientPhone: clientPhone,
      dates: dates,
      location: location,
      notes: notes,
      eventType: eventType
    };

    // 1. Send to photographer's private studio topic
    const topic = `studio_${studioSlug.toLowerCase().replace(/[^a-z0-9]/g, "")}`;
    try {
      const topicMessage = {
        topic: topic,
        notification: {
          title: title,
          body: body
        },
        data: payloadData,
        android: {
          priority: "high",
          notification: {
            sound: "default",
            channelId: "maruti_booking_alerts_v8",
            priority: "max",
            defaultSound: true,
            defaultVibrateTimings: true
          }
        }
      };

      const response = await admin.messaging().send(topicMessage);
      console.log(`Successfully sent FCM message to topic [${topic}]:`, response);
    } catch (err) {
      console.warn(`Error sending FCM to topic [${topic}]:`, err.message);
    }

    // 2. Also send directly to device token if present in /studios/{studioSlug}/profile/fcmToken
    try {
      const tokenDoc = await admin
        .firestore()
        .doc(`studios/${studioSlug}/profile/fcmToken`)
        .get();

      const fcmToken = tokenDoc.exists ? (tokenDoc.data()?.token || tokenDoc.data()?.fcmToken) : null;
      if (fcmToken) {
        const directMessage = {
          token: fcmToken,
          notification: {
            title: title,
            body: body
          },
          data: payloadData,
          android: {
            priority: "high",
            notification: {
              sound: "default",
              channelId: "maruti_booking_alerts_v8",
              priority: "max",
              defaultSound: true,
              defaultVibrateTimings: true
            }
          }
        };

        const directResp = await admin.messaging().send(directMessage);
        console.log(`Successfully sent FCM direct message to token:`, directResp);
      }
    } catch (err) {
      console.warn(`Error sending direct token FCM for studio [${studioSlug}]:`, err.message);
    }
  }
);
