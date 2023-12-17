export interface MovieRating {
  appId: string;
  rating: number;
}

export interface Movie {
    index: number;
    title: string;
    overview: string;
    full_poster_path: string;
    explanation?: string; // Explanation is optional
}