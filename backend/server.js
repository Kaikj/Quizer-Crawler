var express = require('express'),
	bodyParser = require('body-parser');

var allowCrossDomain = function(req, res, next) {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Content-Length, X-Requested-With, auth_token');

  // intercept OPTIONS method
  if ('OPTIONS' == req.method) {
    res.sendStatus(200);
  }
  else {
    next();
  }
};

var app = express();
app.use(bodyParser());
app.use(allowCrossDomain);

var env = app.get('env') == 'development' ? 'dev' : app.get('env');
var port = process.env.PORT || 8081;

// IMPORT MODELS
// =============================================================================
var Sequelize = require('sequelize');

// db config
var env = "dev";
var config = require('./database.json')[env];
var password = config.password ? config.password : null;

// initialize database connection
var sequelize = new Sequelize(
	config.database,
	config.user,
	config.password,
	{
		logging: console.log,
		define: {
			timestamps: false
		}
	}
);

var crypto = require('crypto');
var DataTypes = require("sequelize");

var Seed = sequelize.define('seed_urls', {
    url: {
      type: DataTypes.STRING,
      allowNull: false,
      primaryKey: true
    }
});

var Sentence = sequelize.define('sentences', {
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
}, {
  instanceMethods: {
    retrieveAll: function(onSuccess, onError) {
      Sentence.findAll({}, {raw: true})
        .success(onSuccess).error(onError); 
    }
  }
});

var Visited = sequelize.define('visited_urls', {
    url: {
      type: DataTypes.STRING,
      allowNull: false,
      primaryKey: true
    }
});

// =============================================================================
// HELPERS
// =============================================================================
function shuffle(array) {
  var currentIndex = array.length, temporaryValue, randomIndex ;

  // While there remain elements to shuffle...
  while (0 !== currentIndex) {

    // Pick a remaining element...
    randomIndex = Math.floor(Math.random() * currentIndex);
    currentIndex -= 1;

    // And swap it with the current element.
    temporaryValue = array[currentIndex];
    array[currentIndex] = array[randomIndex];
    array[randomIndex] = temporaryValue;
  }

  return array;
}

function getConditionArray(keywords) {
  var conditionString = '';
  for (var i in keywords) {
    conditionString += "sentence LIKE ? "
    if (i < keywords.length - 1) {
      conditionString += 'OR ';
    }
  }
  var conditionArray = [];
  conditionArray.push(conditionString);
  for (var i in keywords) {
    conditionArray.push('% ' + keywords[i] + ' %');
  }
  return conditionArray;
}

function getAnswersConditionArray(answers) {
  var conditionString = '';
  for (var i in answers) {
    conditionString += "sentence LIKE ? "
    if (i < answers.length - 1) {
      conditionString += 'OR ';
    }
  }
  var conditionArray = [];
  conditionArray.push(conditionString);
  for (var i in answers) {
    var partialSentenceArray = answers[i].split('_________');
    if (partialSentenceArray[0].length > partialSentenceArray[1].length) {
      conditionArray.push(partialSentenceArray[0]+'%');
    } else {
      conditionArray.push('%'+partialSentenceArray[1]);
    }
  }
  return conditionArray;
}

function removeWord(sentence, keyword) {
  sentence = sentence.replace(' ' + keyword + ' ', ' _________ ');
  return sentence;
}

function selectSentences(sentences, keywords) {
  var sentencesObj = {};
  var result = [];

  for (var i in keywords) {
    var anySentencesFound = false;
    sentencesObj[keywords[i]] = [];
    for (var j in sentences) {
      if (sentences[j].dataValues.sentence.indexOf(' ' + keywords[i] + ' ') !== -1 || 
        sentences[j].dataValues.sentence.indexOf(keywords[i] + ' ') === 0) {
        anySentencesFound = true;
        sentencesObj[keywords[i]].push({
          sentence: removeWord(sentences[j].dataValues.sentence, keywords[i]),
          keyword: keywords[i]
        });
      }
    }
    if (!anySentencesFound) {
      sentencesObj[keywords[i]].push({
        sentence: 'No results',
        keyword: keywords[i]
      });
    }
  }

  for (var i in sentencesObj) {
    var randomNum = Math.floor(Math.random() * sentencesObj[i].length);
    result.push(sentencesObj[i][randomNum]);
  }
  return result;
}

function selectSentencesKeywordSeperated(sentences, keywords) {
  var sentencesObj = {};
  var result = [];

  for (var i in keywords) {
    var anySentencesFound = false;
    sentencesObj[keywords[i]] = [];
    for (var j in sentences) {
      if (sentences[j].dataValues.sentence.indexOf(' ' + keywords[i] + ' ') !== -1 || 
        sentences[j].dataValues.sentence.indexOf(keywords[i] + ' ') === 0) {
        anySentencesFound = true;
        sentencesObj[keywords[i]].push(
          removeWord(sentences[j].dataValues.sentence, keywords[i])
        );
      }
    }
    if (!anySentencesFound) {
      sentencesObj[keywords[i]].push(
        ''
      );
    }
  }

  for (var i in sentencesObj) {
    var randomNum = Math.floor(Math.random() * sentencesObj[i].length);
    result.push(sentencesObj[i][randomNum]);
  }

  // Shuffle the array so that answers can't be dtermined from URL
  result = shuffle(result);

  return {
    sentences: result,
    keywords: keywords
  };
}

function formatCorrectAnswers(queriedSentences, providedSentences, blankSentences) {
  var replyObj = [];

  for (var i in providedSentences) {
    var found = false;
    var correctAnswer = '';
    var providedSentence = providedSentences[i];
    var posOfKeyword = blankSentences[i].indexOf('_________');
    var inputAnswer = retrieveWordFromSentence(providedSentence, posOfKeyword);

    for (var j in queriedSentences) {
      var queriedSentence = queriedSentences[j].dataValues.sentence;
      if (queriedSentence === providedSentence) {
        found = true;
        correctAnswer = inputAnswer;
      } else {
        var tempCorrectAnswer = retrieveWordFromSentence(queriedSentence, posOfKeyword);

        // Choosing which correct original sentence to extract the keyword
        if (queriedSentence.substring(0, posOfKeyword) === providedSentence.substring(0, posOfKeyword) ) {
          correctAnswer = retrieveWordFromSentence(queriedSentence, posOfKeyword);
        }
      }
    }

    if (found) {
      replyObj.push({
        key: i,
        correct: true,
        correctKeyword: correctAnswer
      });
    } else {
      replyObj.push({
        key: i,
        correct: false,
        correctKeyword: correctAnswer
      });
    }
  }

  return replyObj
}

function retrieveWordFromSentence(sentence, startPos) {
  var word = '';
  for (var i = startPos; i < sentence.length; i++) {
    if (sentence[i] === ' ') {
      break;
    } else {
      word += sentence[i];
    }
  }
  return word;
}


// =============================================================================
// IMPORT ROUTES
// =============================================================================
var router = express.Router();

// on routes that end in /sentences
// ----------------------------------------------------
router.route('/sentences').post(function(req, res) {

  var keywords = req.body.data;
  var conditionArray = getConditionArray(keywords);

  Sentence.findAll({
    where: conditionArray
  }).then(function(sentences) {
    if (sentences) {
      var result = selectSentences(sentences, keywords);
      res.json(result);
    } else {
      res.send(401, "No sentences found.");
    }
  }, function(error) {
    res.send("Result not found");
  });

})

router.route('/quiz').post(function(req, res) {

  var keywords = req.body.data;
  var conditionArray = getConditionArray(keywords);

  Sentence.findAll({
    where: conditionArray
  }).then(function(sentences) {
    if (sentences) {
      var result = selectSentencesKeywordSeperated(sentences, keywords);
      res.json(result);
    } else {
      res.send(401, "No sentences found.");
    }
  }, function(error) {
    res.send("Result not found");
  });

})

router.route('/quiz/check').post(function(req, res) {
  var answers = req.body.data.sentences;
  var originalSentences = req.body.data.originalSentences;

  Sentence.findAll({
    where: getAnswersConditionArray(originalSentences)
  }).then(function(sentences) {
    if (sentences) {
      var reply = formatCorrectAnswers(sentences, answers, originalSentences);

      res.json(reply);
    } else {
      res.send(401, "No sentences found.");
    }
  }, function(error) {
    res.send("Result not found");
  });
})

// Middleware to use for all requests
router.use(function(req, res, next) {
  // do logging
  console.log('Something is happening.');
  next();
});


// REGISTER OUR ROUTES
// =============================================================================
app.use('/api', router);

// START THE SERVER
// =============================================================================
app.listen(port);
console.log('Server started on port ' + port);

