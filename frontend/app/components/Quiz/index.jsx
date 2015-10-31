import React from 'react';
import Sentence from '../Sentence';

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

    render() {
        var sentencesArray = [];
        console.log(this.state.data);
        if (this.state.data.sentences) {
            for (var i in this.state.data.sentences) {
                if (this.state.data.sentences[i]) {
                    sentencesArray.push(<Sentence
                        sentence={this.state.data.sentences[i]}
                        key={i}
                        />);
                }
            }
        }

        return <div className={styles.main}>
            <div className={styles.wrap}>
                <main className={styles.body}>
                    {sentencesArray}
                </main>
                <button className="btn btn-default btn-lg">Submit</button>
            </div>
        </div>;
    }
}
