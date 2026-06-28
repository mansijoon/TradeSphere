const EventLog = require("../models/EventLog");

class EventLogRepository {
  async create(data, session = null) {
    const log = new EventLog(data);
    return log.save({ session });
  }

  async findByUserId(userId) {
    return EventLog.find({ userId }).sort({ createdAt: -1 });
  }
}

module.exports = new EventLogRepository();
