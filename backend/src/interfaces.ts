export interface MovieRating {
  appId: string;
  //roomId: number;
  rating: number;
}

export interface Movie {
    id: number;
    title: string;
    overview: string;
    full_poster_path: string;
    explanation?: string; // Explanation is optional
}