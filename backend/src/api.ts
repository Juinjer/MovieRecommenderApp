import * as request from 'request';
require('dotenv').config();

let numberOfMovies = 10;  // Set the desired number of movies

const options: request.Options = {
  method: 'GET',
  url: `http://localhost:8000/random/${numberOfMovies}`,
  headers: {
    // Add any headers you might need for your FastAPI server
  }
};

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



async function requestRandomMovies(): Promise<request.Response> {
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

export async function randomMovies() {
	let resp  = await requestRandomMovies();
    const jsonResponse = JSON.parse(String(resp));
    console.log(jsonResponse)
    const jsonArray = [];

     for (let i = 0; i < jsonResponse.movies.length; i++) {
        const movie = jsonResponse.movies[i];
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

export async function fetchSimilarMovies(movieTitle: string) {
  const response = await fetch('http://localhost:8000/simple_recommendation', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({"title": movieTitle }),
  });

  const movies = await response.json();
  console.log(movies)
  return movies;
}