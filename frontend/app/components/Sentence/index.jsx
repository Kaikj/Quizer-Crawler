import React from 'react';
import styles from './style';

export default class Sentence extends React.Component {
    render() {
        return <div className="sentence-container">
            <h3 className="sentence-keyword">{this.props.keyword}</h3>
            <h4 className="sentence-actual">{this.props.sentence}</h4>
        </div>;
    }
}
