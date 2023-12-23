import * as request from 'request';
import {Movie} from './interfaces'
/*
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

export async function getSimpleRecommendation(movieTitle: string) {

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

export async function getFullRecommendation(movieTitle:string){
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
*/

/**
 * Retrieves movie suggestions based on the provided movie title.
 *
 * @returns A Promise that resolves to an array of Movie objects representing the recommended movies.
 * @throws If there is an error during the fetch operation or if the response does not match the expected structure.
 */
export async function getSuggestions(movieTitle: string): Promise<Movie[]>{
    try {
        const response = await fetch('http://127.0.0.1:8000/full_recommendation', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ "title": movieTitle }),
        });

        if (!response.ok) {
          console.error(`Request failed with status ${response.status}`);
            //throw new Error(`Request failed with status ${response.status}`);
        }

        const responseJSON = await response.json();
        const suggestions: Movie[] = responseJSON.recommendations.map((movie: any) => {
            // Map each movie to a Movie object
            return {
                index: movie.index,
                title: movie.title,
                overview: movie.overview,
                full_poster_path: movie.full_poster_path,
                explanation: movie.explanation
            };
        });

        return suggestions;
    } catch (error) {
        console.log("test");
        console.error(error);
        return [];
    }
}


/**
 * Retrieves a number of random movie suggestions.
 *
 * @returns A Promise that resolves to an array of length 'numberOfSuggestions' of Movie objects representing the recommended movies.
 * @throws If there is an error during the fetch operation or if the response does not match the expected structure.
 */
export async function getSuggestionsRandom(numberOfSuggestions: number): Promise<Movie[]> {
    try{
        const response = await fetch(`http://127.0.0.1:8000/random/${numberOfSuggestions}`, {
            method: 'GET',
        });
        const responseJSON = await response.json();
        const suggestions: Movie[] = responseJSON.movies.map((movie: any) => {
            // Map each movie to a Movie object
            return {
                index: movie.index,
                title: movie.title,
                overview: movie.overview,
                full_poster_path: movie.full_poster_path,
            };
        });

        return suggestions;
    } catch (error) {
        console.log("test");
        console.error(error);
        return [];
    }
}
