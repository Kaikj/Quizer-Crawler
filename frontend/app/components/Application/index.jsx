import React from 'react';
import Header from '../Header';
import Sentence from '../Sentence';
import Select from 'react-select';

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
            searchValue: '',
            url: ''
        };
        this.getSentences = this.getSentences.bind(this);
    }

    getSentences() {
        var self = this;

        var apiurl = 'http://localhost:8081';
        if (window.location.hostname === 'quizecrawler-i.comp.nus.edu.sg') {
            apiurl = 'quizecrawler-i.comp.nus.edu.sg';
        }

        // Perform the ajax request to get the questions
        $.ajax({
            url: apiurl + '/api/sentences',
            dataType: 'json',
            type: 'POST',
            data: {
                data: $('.select-box > input').val().split(',')
            },
            success: function(data) {
                self.setState({
                    sentences: (data) ? data : '',
                    searchValue: $('.select-box > input').val(),
                    url: apiurl + '/#/quiz/'+$('.select-box > input').val()
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
        if (this.state.sentences) {
            for (var i in this.state.sentences) {
                sentencesArray.push(<Sentence
                    sentence={this.state.sentences[i].sentence}
                    key={i}
                    keyword={this.state.sentences[i].keyword}
                    />);
            }
        }

        var url = [];
        if (this.state.url) {
            $('.quiz-url').val(this.state.url);
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
                        placeholder="Press [enter] to choose word of choice..."
                        options={options}
                        multi={true}
                        allowCreate={true}
                        value={this.state.searchValue}
                        />
                    <br />
                    <div class="input-group">
                        <input type="text" className="form-control quiz-url" placeholder="URL to quiz will be here after generation..."/>
                    </div>
                    <button className="btn btn-default btn-lg" onClick={this.getSentences}>Generate!</button>
                </main>
                {sentencesArray}
            </div>
        </div>;
    }
}
