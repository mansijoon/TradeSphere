const EventLogRepository = require("../repositories/EventLogRepository");

class EventLogService {
  async logEvent(userId, eventType, payload = {}, session = null) {
    return EventLogRepository.create(
      {
        userId,
        eventType,
        payload,
      },
      session
    );
  }

  async getLogs(userId) {
    return EventLogRepository.findByUserId(userId);
  }
}

module.exports = new EventLogService();
