import React from 'react';
import Header from '../Header';
import Sentence from '../Sentence';
import Select from 'react-select';
import $ from 'jquery';

/**
 * Import locally scoped styles using css-loader
 * See style.sass in this directory.
 *
 * More info: https://github.com/webpack/css-loader#local-scope
 */
import styles from './style';

export default class Application extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            sentences: '',
            searchValue: ''
        };
        this.getSentences = this.getSentences.bind(this);
    }

    getSentences() {
        var self = this;
        // Perform the ajax request to get the questions
        $.ajax({
            url: 'http://localhost:8081/api/sentences',
            dataType: 'json',
            type: 'POST',
            data: {
                data: $('.select-box > input').val().split(',')
            },
            success: function(data) {
                self.setState({
                    sentences: (data) ? data : '',
                    searchValue: $('.select-box > input').val()
                });
            }.bind(this),
            error: function(xhr, status, err) {
                console.error('http://localhost:8081/api/sentences', status, err.toString());
            }.bind(this)
        });
    }

    render() {
        var options = [
            {value: 'networking', label: 'networking'},
            {value: 'rocks', label: 'rocks'}
        ];
        var sentencesArray = [];
        console.log(this.state);
        if (this.state.sentences) {
            for (var i in this.state.sentences) {
                sentencesArray.push(<Sentence
                    sentence={this.state.sentences[i].sentence}
                    key={i}
                    keyword={this.state.sentences[i].keyword}
                    />);
            }
        }

        return <div className={styles.main}>
            <div className={styles.wrap}>
                <Header />

                <main className={styles.body}>
                    <h3>Enter in keywords to generate quiz:</h3>
                    <br />
                    <Select
                        className="select-box"
                        name="form-field-name"
                        options={options}
                        multi={true}
                        allowCreate={true}
                        value={this.state.searchValue}
                        />
                    <br />
                    <button onClick={this.getSentences}>Generate!</button>
                </main>
                {sentencesArray}
            </div>
        </div>;
    }
}
