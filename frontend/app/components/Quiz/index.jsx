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
        // Get all sentences
        var sentencesLength = $('.sentence-actual').length;
        var data = [];
        for (var i = 0; i < sentencesLength; i++) {
            data.push({
                answer: $($('.sentence-actual').get(0)).html(),
                key: i
            });
        }
        console.log(data);
    }

    onDrop(data) {
        let answer = data.answer;
        let question = $('.Droppable.over > div > h4');
        let questionString = question.html();
        questionString = questionString.replace('_________', answer);
        question.html(questionString);
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
                answers.push(<Draggable key={i} type="answer" data={keywords[i]}><li>{keywords[i]}</li></Draggable>);
            }
        }

        return <div className={styles.main}>
            <div className={styles.wrap}>
                <div>
                    <ul>
                        {answers}
                    </ul>
                </div>
                <main className={styles.body}>
                    {sentencesArray}
                </main>
                <button className="btn btn-default btn-lg" onClick={this.submitAnswers}>Submit</button>
            </div>
        </div>;
    }
}
