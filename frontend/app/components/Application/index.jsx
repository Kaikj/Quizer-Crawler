import React from 'react';
import Header from '../Header';
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
    getSentences() {
        console.log($('.select-box > input').val().split(','));
        $.ajax({
            url: 'http://localhost:8081/api/sentences',
            dataType: 'json',
            type: 'POST',
            data: {
                data: $('.select-box > input').val().split(',')
            },
            success: function(data) {
                console.log(data);
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

        return <div className={styles.main}>
            <div className={styles.wrap}>
                <Header />

                <main className={styles.body}>
                    <h3>Enter in keywords to generate quiz:</h3>
                    <Select
                        className="select-box"
                        name="form-field-name"
                        options={options}
                        multi={true}
                        allowCreate={true}
                        />
                    <button onClick={this.getSentences}>Generate!</button>
                </main>
            </div>
        </div>;
    }
}
