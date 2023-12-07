import * as request from 'request';
require('dotenv').config();

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

async function requestMovie(): Promise<request.Response> {
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

export async function randomMovie() {
	let resp  = await requestMovie();
	const jsonResponse = JSON.parse(String(resp));
    let img:String = jsonResponse["results"][0]["primaryImage"]["url"];
	let title: String = jsonResponse["results"][0]["titleText"]["text"];
	let desc: String = jsonResponse["results"][0]["plot"]["plotText"]["plainText"];
	let id: String = jsonResponse["results"][0]["id"];
	console.log({img:img,title:title,desc:desc,id:id});
	return {img:img,title:title,desc:desc,id:id};
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