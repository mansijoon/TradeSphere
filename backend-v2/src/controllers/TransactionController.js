const TransactionService = require("../services/TransactionService");

class TransactionController {
  async getTransactions(req, res) {
    try {
      const transactions =
        await TransactionService.getTransactions(
          req.user.userId
        );

      res.status(200).json(transactions);
    } catch (error) {
      res.status(400).json({
        error: error.message,
      });
    }
  }
}

module.exports = new TransactionController();
