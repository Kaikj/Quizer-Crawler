/* jshint indent: 2 */

module.exports = function(sequelize, DataTypes) {
  return sequelize.define('sentences', {
    sentence: {
      type: DataTypes.STRING,
      allowNull: false
    },
    url: {
      type: DataTypes.STRING,
      allowNull: false
    },
    accept: {
      type: DataTypes.BOOLEAN,
      allowNull: true
    }
  });
};
