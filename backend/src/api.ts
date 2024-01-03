import * as request from 'request';
import {Movie} from './interfaces'

const endpoint = process.env.REC || "127.0.0.1";

/**
 * Retrieves a number of random movie suggestions.
 *
 * @returns A Promise that resolves to an array of length 'numberOfSuggestions' of Movie objects representing the random movies.
 * @throws If there is an error during the fetch operation or if the response does not match the expected structure.
 */
export async function getSuggestionsRandom(numberOfSuggestions: number): Promise<Movie[]> {
    try{
        const response = await fetch(`http://${endpoint}:8000/random/${numberOfSuggestions}`, {
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

/**
 * Retrieves the 3 most similar movies for a specific movie title
 *
 * @returns A Promise that resolves to an array of 3 Movie objects representing the recommended movies.
 * @throws If there is an error during the fetch operation or if the response does not match the expected structure.
 */
export async function get3NN(movieTitle: string): Promise<Movie[]> {
    try {
        const response = await fetch(`http://${endpoint}:8000/3nn`, {
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
                full_poster_path: movie.full_poster_path
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
 * Retrieves the explanation why a child movie is similar to a specific parent movie
 *
 * @returns A Promise that resolves to a Movie object inlucding an explanation.
 * @throws If there is an error during the fetch operation or if the response does not match the expected structure.
 */
export async function getNeighbourExplanation(parent: Movie, child: Movie): Promise<Movie> {
    console.log(JSON.stringify({ "parent": parent, "child": child }));
    try {
        const response = await fetch(`http://${endpoint}:8000/neighbour_explanation`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                "parent": {
                    "index": parent.index,
                    "title": parent.title,
                    "overview": parent.overview,
                    "full_poster_path": parent.full_poster_path,
                    "explanation": ""
                },  
                "child": {
                    "index": child.index, 
                    "title": child.title,
                    "overview": child.overview,
                    "full_poster_path": child.full_poster_path,
                    "explanation": ""
                }

             }),
        });

        if (!response.ok) {
          console.error(`Request failed with status ${response.status}`);
            throw new Error(`Request failed with status ${response.status}`);
        }

        const responseJSON = await response.json();

        return {
            index: responseJSON.recommendation.index,
            title: responseJSON.recommendation.title,
            overview: responseJSON.recommendation.overview,
            full_poster_path: responseJSON.recommendation.full_poster_path,
            explanation: responseJSON.recommendation.explanation
        };
    } catch (error) {
        console.log("test");
        console.error(error);
        return {"index": 0, "title": "", "overview": "", "full_poster_path": "", "explanation": ""};
    }
}

/**
 * Retrieves movie suggestions based on the provided movie title.
 *
 * @returns A Promise that resolves to an array of Movie objects representing the recommended movies.
 * @throws If there is an error during the fetch operation or if the response does not match the expected structure.
 */
// export async function getSuggestions(movieTitle: string): Promise<Movie[]>{
//     try {
//         const response = await fetch(`http://${endpoint}:8000/full_recommendation`, {
//             method: 'POST',
//             headers: {
//                 'Content-Type': 'application/json',
//             },
//             body: JSON.stringify({ "title": movieTitle }),
//         });

//         if (!response.ok) {
//           console.error(`Request failed with status ${response.status}`);
//             //throw new Error(`Request failed with status ${response.status}`);
//         }

//         const responseJSON = await response.json();
//         const suggestions: Movie[] = responseJSON.recommendations.map((movie: any) => {
//             // Map each movie to a Movie object
//             return {
//                 index: movie.index,
//                 title: movie.title,
//                 overview: movie.overview,
//                 full_poster_path: movie.full_poster_path,
//                 explanation: movie.explanation
//             };
//         });

//         return suggestions;
//     } catch (error) {
//         console.log("test");
//         console.error(error);
//         return [];
//     }
// }