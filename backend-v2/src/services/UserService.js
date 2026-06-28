const bcrypt = require("bcryptjs");

const UserRepository = require("../repositories/UserRepository");
const PortfolioService = require("./PortfolioService");
const { generateToken } = require("../utils/jwt");

class UserService {
  async register(email, password) {
    const existingUser =
      await UserRepository.findByEmail(email);

    if (existingUser) {
      throw new Error(
        "User already exists"
      );
    }

    const passwordHash =
      await bcrypt.hash(password, 10);

    const user =
      await UserRepository.create({
        email,
        passwordHash,
      });

    await PortfolioService.createPortfolio(
      user._id
    );

    return user;
  }

  async login(email, password) {
    const user =
      await UserRepository.findByEmail(email);

    if (!user) {
      throw new Error(
        "Invalid credentials"
      );
    }

    const isMatch =
      await bcrypt.compare(
        password,
        user.passwordHash
      );

    if (!isMatch) {
      throw new Error(
        "Invalid credentials"
      );
    }

    const token = generateToken(user);

    return {
      user,
      token,
    };
  }
}

module.exports = new UserService();
