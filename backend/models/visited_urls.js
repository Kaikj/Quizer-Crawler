/* jshint indent: 2 */

module.exports = function(sequelize, DataTypes) {
  return sequelize.define('visited_urls', {
    url: {
      type: DataTypes.STRING,
      allowNull: false,
      primaryKey: true
    }
  });
};
