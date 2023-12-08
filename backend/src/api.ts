import * as request from 'request';
import { getRandomPositiveRated } from './ratinglogic'
require('dotenv').config();

/*
const options: request.Options = {
  method: 'GET',
  url: 'https://moviesdatabase.p.rapidapi.com/titles/random',
  qs: {
    list: 'top_rated_english_250',
    titleType: 'movie',
    startYear: '2000',
	info: 'base_info'
  },
  headers: {
    'X-RapidAPI-Key': process.env.API_KEY,
    'X-RapidAPI-Host': 'moviesdatabase.p.rapidapi.com'
  }
};
*/

const
async function requestRandomMovies(): Promise<request.Response>{
    return new Promise((resolve, reject) => {
            request(options, (error, response, body) => {
                if (error) {
                    reject(error);
                } else {
                    resolve(body);
                }
            });
	});
}


export async function getRandomMovies(numberOfMovies: number) {
    const response = await fetch(`http://localhost:8000/random/${numberOfMovies}`, {
        method: 'GET',
    });

    const randomMoviesJSON = await response.json();
    console.log(randomMoviesJSON)
    const jsonArray = [];

    for (let i = 0; i < randomMoviesJSON.movies.length; i++) {
        const movie = randomMoviesJSON.movies[i];
        const img = movie.full_poster_path;
        const title = movie.title;
        const desc = movie.overview;

        const movieObject = {
            img: img,
            title: title,
            desc: desc,
        };
        jsonArray.push(movieObject);
    }

    // Print or use jsonArray as needed
    console.log(jsonArray);
    return jsonArray;
}

export async function fetchSimilarMovies(appId: string) {
    const movieTitle = await getRandomPositiveRated(appId);

    const response = await fetch('http://localhost:8000/simple_recommendation', {
        method: 'POST',
        headers: {
        'Content-Type': 'application/json',
        },
        body: JSON.stringify({"title": movieTitle }),
    });

    const similarMoviesJSON = await response.json();
    console.log(similarMoviesJSON)
    return similarMoviesJSON;
}

export async function getFullRecommendation(appId:string){
    const movieTitle = await getRandomPositiveRated(appId);

    const response = await fetch('http://localhost:8000/full_recommendation', {
        method: 'POST',
        headers: {
        'Content-Type': 'application/json',
        },
        body: JSON.stringify({"title": movieTitle }),
    });
    const fullRecommendationJSON = await response.json();
    console.log(fullRecommendationJSON)
    return fullRecommendationJSON;
}