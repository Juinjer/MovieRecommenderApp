import { RateFilmData } from './interfaces';

// Map to store ratings based on appId
const ratingsMap = new Map<string, { positive: RateFilmData[], negative: RateFilmData[] }>();

export async function handleMovieRating(movieRating: RateFilmData) {
    const { roomId, appId, movieTitle, rating } = movieRating;

    // Ensure the appId exists in the map
    if (!ratingsMap.has(appId)) {
        ratingsMap.set(appId, { positive: [], negative: [] });
    }

    // Get the ratings for the appId
    const ratings = ratingsMap.get(appId)!;

    // Check the rating and add to the appropriate list
    if (rating === 1) {
        ratings.positive.push(movieRating);
    } else if (rating === -1) {
        ratings.negative.push(movieRating);
    }

    // You can perform additional actions based on the ratings if needed

    // Log the current ratings for debugging purposes
    console.log(`Positive Ratings for ${appId}:`, ratings.positive);
    console.log(`Negative Ratings for ${appId}:`, ratings.negative);
}

export async function getRandomPositiveRated(appId: string): Promise<string | null>{
    // Check if the appId exists in the map
    if (!ratingsMap.has(appId)) {
        console.error(`No ratings found for appId: ${appId}`);
        return null;
    }

    // Get the positive ratings for the appId
    const positiveRatings = ratingsMap.get(appId)!.positive;

    // Check if there are positive ratings
    if (positiveRatings.length === 0) {
        console.error(`No positive ratings found for appId: ${appId}`);
        return null;
    }

    // Get the movieTitle at the random index
    const randomMovieTitle = positiveRatings[Math.floor(Math.random() * positiveRatings.length)].movieTitle;

    return randomMovieTitle;
}
