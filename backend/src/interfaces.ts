export interface MovieRating {
  appId: string;
  //roomId: number;
  rating: number;
}

export interface Movie {
    index: number;
    title: string;
    overview: string;
    full_poster_path: string;
    explanation?: string; // Explanation is optional
}