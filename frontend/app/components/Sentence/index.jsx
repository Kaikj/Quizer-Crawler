import React from 'react';
import styles from './style';

export default class Sentence extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            originalSentence: this.props.sentence
        };
        this.resetQuestion = this.resetQuestion.bind(this);
    }

    resetQuestion(e) {
        $(e.currentTarget).find('.sentence-actual').html(this.state.originalSentence);
        $(e.currentTarget).removeClass('answered');
    }

    render() {
        return <div className="sentence-container" onClick={this.resetQuestion}>
            <h3 className="sentence-keyword">{this.props.keyword}</h3>
            <h4 className="sentence-actual">{this.props.sentence}</h4>
        </div>;
    }
}
