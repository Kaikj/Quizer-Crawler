import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import Sentence from '../Sentence';
import { Draggable, Droppable } from 'react-drag-and-drop';
import Sticky from 'react-sticky';

/**
 * Import locally scoped styles using css-loader
 * See style.sass in this directory.
 *
 * More info: https://github.com/webpack/css-loader#local-scope
 */
import styles from './style';

export default class Quiz extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            data: ''
        };
        this.getSentences = this.getSentences.bind(this);
        this.submitAnswers = this.submitAnswers.bind(this);
        this.indicateCorrectAndWrongAnswers = this.indicateCorrectAndWrongAnswers.bind(this);
    }

    componentDidMount() {
        let { queryString } = this.props.params;
        this.getSentences(queryString);
    }

    getSentences(query) {
        var self = this;
        // Perform the ajax request to get the questions
        $.ajax({
            url: 'http://localhost:8081/api/quiz',
            dataType: 'json',
            type: 'POST',
            data: {
                data: query.split(',')
            },
            success: function(data) {
                self.setState({
                    data: (data) ? data : ''
                });
            }.bind(this),
            error: function(xhr, status, err) {
                console.error('http://localhost:8081/api/sentences', status, err.toString());
            }.bind(this)
        });
    }

    submitAnswers() {
        var self = this;

        // Get all sentences
        var sentencesLength = $('.sentence-actual').length;
        var data = {};
        data.sentences = [];
        for (var i = 0; i < sentencesLength; i++) {
            data.sentences.push($($('.sentence-actual').get(i)).html().replace('&nbsp;',' '));
        }
        data.originalSentences = this.state.data.sentences;

        $.ajax({
            url: 'http://localhost:8081/api/quiz/check',
            dataType: 'json',
            type: 'POST',
            data: {
                data: data
            },
            success: function(data) {
                self.indicateCorrectAndWrongAnswers(data);
            }.bind(this),
            error: function(xhr, status, err) {
                console.error('http://localhost:8081/api/sentences', status, err.toString());
            }.bind(this)
        });
    }

    indicateCorrectAndWrongAnswers(data) {
        var numOfCorrect = 0;
        for (var i in data) {
            var correctAnswer = data[i].correctKeyword;
            if (data[i].correct) {
                numOfCorrect++;
                $($('.sentence-actual').get(data[i].key)).parent().addClass('correct-answer');
            } else {
                $($('.sentence-actual').get(data[i].key)).parent().addClass('wrong-answer');
                $($('.sentence-actual').get(data[i].key)).parent().find('.sentence-keyword').html(correctAnswer);
            }
        }

        $('.score-container').html('Your score: ' + numOfCorrect + ' out of ' + data.length);
    }

    onDrop(data, e) {
        let answer = data.answer;
        let question = $(e.currentTarget);
        let questionString = question.html();
        questionString = questionString.replace('_________', answer);
        question.html(questionString);
    }

    refresh() {
        location.reload();
    }

    render() {
        var sentencesArray = [];
        if (this.state.data.sentences) {
            for (var i in this.state.data.sentences) {
                if (this.state.data.sentences[i]) {
                    sentencesArray.push(
                        <Droppable
                            types={['answer']}
                            onDrop={this.onDrop.bind(this)}
                            key={i}>
                            <Sentence sentence={this.state.data.sentences[i]} key={i} />
                        </Droppable>
                    );
                }
            }
        }

        let answers = [];
        let keywords = this.state.data.keywords;
        if (keywords) {
            for (let i in keywords) {
                answers.push(<Draggable key={i} type="answer" data={keywords[i]}><div className="quiz-options">{keywords[i]}</div></Draggable>);
            }
        }

        return <div className={styles.main}>
            <div className={styles.wrap}>
                <Sticky className="row">
                    {answers}
                </Sticky>
                <main className={styles.body}>
                    {sentencesArray}
                </main>
                <div className="score-container">

                </div>
                <div className="button-container">
                    <button className="btn btn-default btn-lg" onClick={this.refresh}>Retry</button>
                    <button className="btn btn-default btn-lg" onClick={this.submitAnswers}>Submit</button>
                </div>
            </div>
        </div>;
    }
}
