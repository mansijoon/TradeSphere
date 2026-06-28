const UserService = require("../services/UserService");

class AuthController {
  async register(req, res) {
    try {
      const { email, password } = req.body;

      const user =
        await UserService.register(
          email,
          password
        );

      res.status(201).json({
        message:
          "User registered successfully",
        userId: user._id,
      });
    } catch (error) {
      res.status(400).json({
        error: error.message,
      });
    }
  }

  async login(req, res) {
    try {
      const { email, password } = req.body;

      const result =
        await UserService.login(
          email,
          password
        );

      res.status(200).json({
        message: "Login successful",
        token: result.token,
        userId: result.user._id,
      });
    } catch (error) {
      res.status(401).json({
        error: error.message,
      });
    }
  }
}

module.exports = new AuthController();
