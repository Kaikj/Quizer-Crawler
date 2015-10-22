/*! React Starter Kit | MIT License | http://www.reactstarterkit.com/ */

import React, { PropTypes, Component } from 'react';
import withStyles from '../../decorators/withStyles';
import styles from './CreateQuiz.css';
import Select from 'react-select';

@withStyles(styles)
class CreateQuiz extends Component {

  static contextTypes = {
    onSetTitle: PropTypes.func.isRequired,
  };

  render() {
    const options = [
      { value: 'banana', label: 'banana' },
      { value: 'potato', label: 'potato' },
    ];

    const title = 'Create a Quiz now:';
    this.context.onSetTitle(title);
    return (
      <div className="CreateQuiz">
        <div className="CreateQuiz-container">
          <h1>{title}</h1>
          <Select
            name="form-field-name"
            options={options}
            multi={true}
            allowCreate={true}
            />
        </div>
      </div>
    );
  }

}

export default CreateQuiz;
