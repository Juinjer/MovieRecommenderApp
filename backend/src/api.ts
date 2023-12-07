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

async function requestMovies(): Promise<request.Response> {
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
    let resp  = await requestMovies();
    const jsonResponse = JSON.parse(String(resp));
    const jsonArray = [];

    for(let i = 0;  i < jsonResponse["results"].length; i++){
        let img:String = jsonResponse["results"][i]["primaryImage"]["url"];
        let title: String = jsonResponse["results"][i]["titleText"]["text"];
        let desc: String = jsonResponse["results"][i]["plot"]["plotText"]["plainText"];

        const jsonStringMovie = JSON.stringify({img: img, title: title, desc: desc,});
        jsonArray.push(jsonStringMovie);
    }
    console.log(jsonArray.toString())
    return jsonArray;

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