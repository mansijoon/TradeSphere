const Transaction =
  require("../models/Transaction");

class TransactionRepository {
  async create(
    transactionData,
    session = null
  ) {
    const transaction =
      new Transaction(
        transactionData
      );

    return transaction.save({
      session,
    });
  }

  async findByUserId(userId) {
    return Transaction.find({
      userId,
    }).sort({
      createdAt: -1,
    });
  }
}

module.exports =
  new TransactionRepository();
