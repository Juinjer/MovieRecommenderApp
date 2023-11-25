import express from 'express';
import {router} from './route.js';

const app = express();

app.listen(8080);

app.use("/api", router);

