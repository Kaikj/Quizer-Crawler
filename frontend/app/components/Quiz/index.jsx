import React, { PropTypes } from 'react';
import Sentence from '../Sentence';
import { Draggable, Droppable } from 'react-drag-and-drop';

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
        var data = [];
        for (var i = 0; i < sentencesLength; i++) {
            data.push($($('.sentence-actual').get(i)).html().replace('&nbsp;',' '));
        }

        console.log(data);

        $.ajax({
            url: 'http://localhost:8081/api/quiz/check',
            dataType: 'json',
            type: 'POST',
            data: {
                data: data
            },
            success: function(data) {
                console.log(data);
                self.indicateCorrectAndWrongAnswers(data);
            }.bind(this),
            error: function(xhr, status, err) {
                console.error('http://localhost:8081/api/sentences', status, err.toString());
            }.bind(this)
        });
    }

    indicateCorrectAndWrongAnswers(data) {
        for (var i in data) {
            if (data[i].correct) {
                $($('.sentence-actual').get(data[i].key)).parent().addClass('correct-answer');
            } else {
                $($('.sentence-actual').get(data[i].key)).parent().addClass('wrong-answer');
            }
        }
    }

    onDrop(data) {
        let answer = data.answer;
        let question = $('.Droppable.over > div > h4');
        let questionString = question.html();
        questionString = questionString.replace('_________', answer);
        question.html(questionString);
        question.parent().parent().removeClass('over');
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
                <div className="row">
                    {answers}
                </div>
                <main className={styles.body}>
                    {sentencesArray}
                </main>
                <button className="btn btn-default btn-lg" onClick={this.submitAnswers}>Submit</button>
            </div>
        </div>;
    }
}
