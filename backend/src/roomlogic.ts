import { MovieRating, Movie} from './interfaces'
import { get3NN, getNeighbourExplanation, getSuggestionsRandom} from './api';
import { notifyProcessingDone} from './sio';

export class Room {
    private host: string;
    private roomId: number;
    private members: string[];
    private names: Map<string, string>;

    private status: boolean = false;
    //TODO: can be changed to a higher number
    private numberOfRecommendations: number = 5;
    private numberOfSwipes: number = 3;

    private nextSuggestions: Movie[] = [];
    private movieRatings: Map<Movie, MovieRating[]> = new Map<Movie, MovieRating[]>()
    private topRecommendation: Movie[] = [];

    private namesOptions: string[] = [
        "Whispering Fox", "Silent Phoenix", "Mystery Hawk", "Shadowed Tiger",
        "Unknown Sparrow", "Veiled Cobra", "Masked Griffin", "Ghostly Pegasus",
        "Cloaked Raven", "Enigma Lynx"
    ];

    constructor(host: string) {
        this.roomId = this.generateId();
        this.host = host;
        this.members = [host];

        this.names = new Map();
        this.names.set(host, this.getName());

        (async () => {
            this.nextSuggestions = await getSuggestionsRandom(this.numberOfSwipes);
        })();
    }

    addMember(member: string): void {
        console.log("addmember:" + member);
        this.members.push(member);
        this.names.set(member, this.getName());
    }

    removeMember(member: string): void {
        const memberIndex = this.members.indexOf(member);

        if (memberIndex !== -1) {
            this.members.splice(memberIndex, 1);

            if (this.names.has(member)) {
                this.names.delete(member);
            }
        }
    }

    generateId(): number {
        let min = 100000;
        let max = 999999;
        return Math.floor(Math.random() * (max - min) + min);
    }

    //** Rating logic **//
    getMovieRatings(): Map<Movie, MovieRating[]>{
        return this.movieRatings;
    }

    isAllRatingsSubmitted(): boolean{
        const totalAmountRatings = Array.from(this.movieRatings.values()).length;
        const expectedTotalRatings = this.members.length * this.numberOfSwipes;
        // uses modulo because this allows us to check for multiple rounds
        return totalAmountRatings % expectedTotalRatings === 0;
    }

    //Potential for concurrency problems!
    async addMovieRating(movie: Movie, rating: MovieRating): Promise<void>{
        let ratings: MovieRating[] = this.movieRatings.get(movie) || [];
        ratings.push(rating); // This is safe because ratings is guaranteed to be an array
        this.movieRatings.set(movie, ratings);

        if (this.isAllRatingsSubmitted()) {
            this.setupScoreMovieArrays();
            await this.getExplanations();
        }
    }

    private combinedRatings = new Map<Movie,number>();
    // set up the combined ratings map for each unique movie
    setCombinedRatings() {
        let moviesIndices = new Map<number,Movie>();
        for (let [movie, ratings] of this.movieRatings) {
            let movieId = movie.id;
            let movieToUse = moviesIndices.has(movieId) ? moviesIndices.get(movieId) as Movie : movie;
            moviesIndices.set(movieId,movieToUse);
            let existingRating = this.combinedRatings.get(movieToUse) || 0;
            for (let rating of ratings) {
                existingRating += rating.rating;
            }
            this.combinedRatings.set(movieToUse, existingRating);
        }
    }

    private perfectScoreMovies: Movie[] = [];
    private secondHighestScoreMovies: Movie[] = [];
    private thirdHighestScoreMovies: Movie[] = [];
    // set up the movie arrays based on the combined ratings
    setupScoreMovieArrays() {
        this.setCombinedRatings();

        let perfectScore = this.members.length;
        let scores = Array.from(this.combinedRatings.values()).sort((a, b) => b - a);
        let secondHighestScore = scores[1];
        let thirdHighestScore = scores[2];

        for (let [movie,rating] of this.combinedRatings) {
            if (rating === perfectScore) {
                movie.explanation = `100% of you liked this movie`
                this.perfectScoreMovies.push(movie);
            } else if (rating === secondHighestScore && secondHighestScore >= 0) {
                this.secondHighestScoreMovies.push(movie);
            } else if (rating === thirdHighestScore && thirdHighestScore > 0) {
                this.thirdHighestScoreMovies.push(movie);
            }
        }
    }

    // remove all other occurrences of a movie in the array once one copy is chosen at random
    removeAllOtherOccurences(array: { parent: Movie, childId: number }[], movie: { parent: Movie, childId: number }) {
        return array.filter((value) => value.childId !== movie.childId);
    }

    private nearestNeighbours: { parent: Movie, childId: number }[] = [];
    async getExplanations(): Promise<void> {
        // perfect score movies are always recommended
        this.topRecommendation.push(...this.perfectScoreMovies);
        // determine how many more movies should be recommended, see TODO above
        let amntRandomMovies = this.numberOfRecommendations - this.topRecommendation.length;
       
        // perfect neighbours get 3 copies, 2nd highest get 2 copies, 3rd highest get 1 copy
        let moviesObject = [
            {movies: this.perfectScoreMovies, copies: 3},
            {movies: this.secondHighestScoreMovies, copies: 2},
            {movies: this.thirdHighestScoreMovies, copies: 1}
        ]
    
        // get the 3 nearest neighbours indices for each movie and fill the array with the correct amount of copies
        for (let moviesObj of moviesObject) {
            for (let movie of moviesObj.movies) {
                let nn_ids = await get3NN(movie.id);
                for (let n_id of nn_ids) {
                    for (let i=0; i<moviesObj.copies; i++) {
                        let movieWithParent = {
                            parent: movie,
                            childId: n_id
                        }
                        this.nearestNeighbours.push(movieWithParent);
                    }
                }
            }
        }
        
        // randomly select movies until enough are selected
        let randomMovies = [];
        for (let i = 0; i < amntRandomMovies; i++) {
            let randomIndex = Math.floor(Math.random() * this.nearestNeighbours.length);
            let selectedMovie = this.nearestNeighbours[randomIndex];
            randomMovies.push(selectedMovie);
            this.nearestNeighbours = this.removeAllOtherOccurences(this.nearestNeighbours,selectedMovie);
        }
    
        // get the explanation for each randomly selected movie
        for (let movie of randomMovies) {
            if (movie !== undefined) {
                let childExplanation = await getNeighbourExplanation(movie.parent.id, movie.childId);
                console.log("roomlogic");
                console.log(childExplanation);
                this.topRecommendation.push(childExplanation);
            }
        }
        notifyProcessingDone(this.members,this.topRecommendation);
    }

    getSuggestions(): Movie[]{
        return this.nextSuggestions;
    }

    getName(): string {
        const availableNames = this.namesOptions.filter(name => ![...(this.names.values() as Iterable<string>)].includes(name));

        if (availableNames.length === 0) {
            return "No available names";
        }

        const randomIndex = Math.floor(Math.random() * availableNames.length);
        return availableNames[randomIndex];
    }

    getHost(): string {
        return this.host;
    }

    getMembers(): string[] {
        return this.members.slice(); // Return a copy to prevent external modification
    }

    getNames(): Map<string, string> {
        return new Map(this.names); // Return a copy to prevent external modification
    }

    getRoomId(): number {
        return this.roomId;
    }

    getNSwipes(): number {
        return this.numberOfSwipes;
    }

    setNSwipes(n: number): void {
        this.numberOfSwipes = n;
        (async () => {
            this.nextSuggestions = await getSuggestionsRandom(this.numberOfSwipes);
        })();
    }
    getStatus() {
        return this.status;
    }
    start() {
        this.status = true;
    }
}
