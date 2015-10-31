// IMPORTANT: This needs to be first (before any other components)
// to get around CSS order randomness in webpack.
import './css/base';

// Some ES6+ features require the babel polyfill
// More info here: https://babeljs.io/docs/usage/polyfill/
// Uncomment the following line to enable the polyfill
// require("babel/polyfill");

import React from 'react';
import ReactDOM from 'react-dom';
import Application from './components/Application';
import Quiz from './components/Quiz';
import { Router, Route, Link } from 'react-router';

ReactDOM.render((
    <Router>
        <Route path="/" component={Application}/>
        <Route path="/quiz/:queryString" component={Quiz}/>
    </Router>
), document.getElementById('app'));
