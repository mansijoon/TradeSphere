const TransactionRepository = require("../repositories/TransactionRepository");

class TransactionService {
  async createTransaction(
    data,
    session = null
  ) {
    return TransactionRepository.create(
      data,
      session
    );
  }

  async getTransactions(userId) {
    return TransactionRepository.findByUserId(
      userId
    );
  }
}

module.exports = new TransactionService();
