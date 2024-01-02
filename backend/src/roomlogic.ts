import { MovieRating, Movie} from './interfaces'
import { get3NN, getNeighbourExplanation, getSuggestions, getSuggestionsRandom} from './api';
import { notifyProcessingDone} from './sio';

export class Room {
    private host: string;
    private roomId: number;
    private members: string[];
    private names: Map<string, string>;

    private status: boolean = false;
    private numberOfRecommendations: number = 5;
    private numberOfSwipes: number = 3;
    private likeThreshold: number = 0.75;

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
  /*
        this.numberOfRecommendations = 5;
        this.numberOfSwipes = 3;
        this.likeThreshold = 0.75;
  */
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
        //console.log("add rating:\n")
        //console.log(movie, rating);
        let ratings: MovieRating[] = this.movieRatings.get(movie) || [];
        ratings.push(rating); // This is safe because ratings is guaranteed to be an array
        this.movieRatings.set(movie, ratings);

        if (this.isAllRatingsSubmitted()) {
            await this.setupRecommendations();
        }
    }

    private combinedRatings = new Map<Movie,number>();
    setCombinedRatings() {
        let moviesIndices = new Map<number,Movie>();

        for (let [movie, ratings] of this.movieRatings) {
            let movieIndex = movie.index;

            let movieToUse = moviesIndices.has(movieIndex) ? moviesIndices.get(movieIndex) as Movie : movie;
            moviesIndices.set(movieIndex,movieToUse);

            if (!this.combinedRatings.has(movieToUse)) {
                this.combinedRatings.set(movieToUse, 0);
            }

            for (let rating of ratings) {
                let existingRating = this.combinedRatings.get(movieToUse) || 0;
                this.combinedRatings.set(movieToUse, existingRating + rating.rating);
            }
        }
    }

    private nearestNeighbours: { parent: Movie, child: Movie }[] = [];
    async setupRecommendations() {
        this.setCombinedRatings();

        let perfectScoreMovies: Movie[] = [];
        let secondHighestScoreMovies: Movie[] = [];
        let thirdHighestScoreMovies: Movie[] = [];

        let perfectScore = this.members.length;
        let secondHighestScore = Math.max(...Array.from(this.combinedRatings.values()).filter((score: number) => score < perfectScore));
        let thirdHighestScore = Math.max(...Array.from(this.combinedRatings.values()).filter((score: number) => score < secondHighestScore));

        for (let [movie,rating] of this.combinedRatings) {
            if (rating === this.members.length) {
                movie.explanation = `100% of you liked this movie`
                perfectScoreMovies.push(movie);
            } else if (rating === secondHighestScore && secondHighestScore >= 0) {
                secondHighestScoreMovies.push(movie);
            } else if (rating === thirdHighestScore && thirdHighestScore > 0) {
                thirdHighestScoreMovies.push(movie);
            }
        }

        this.topRecommendation.push(...perfectScoreMovies);
        let amntRandomMovies = this.numberOfRecommendations - this.topRecommendation.length;
       
        let moviesObject = [
            {movies: perfectScoreMovies, copies: 3},
            {movies: secondHighestScoreMovies, copies: 2},
            {movies: thirdHighestScoreMovies, copies: 1}
        ]

        for (let moviesObj of moviesObject) {
            for (let movie of moviesObj.movies) {
                let nn = await get3NN(movie.title);
                for (let n of nn) {
                    for (let i=0; i<moviesObj.copies; i++) {
                        let movieWithParent = {
                            parent: movie,
                            child: n
                        }
                        this.nearestNeighbours.push(movieWithParent);
                    }
                }
            }
        }

        function removeAllOtherOccurences(array: { parent: Movie, child: Movie }[], movie: { parent: Movie, child: Movie }) {
            return array.filter((value) => value.child.title !== movie.child.title);
        }

        let randomMovies = [];
        
        for (let i = 0; i < amntRandomMovies; i++) {
            let randomIndex = Math.floor(Math.random() * this.nearestNeighbours.length);
            let selectedMovie = this.nearestNeighbours[randomIndex];
            randomMovies.push(selectedMovie);
            this.nearestNeighbours = removeAllOtherOccurences(this.nearestNeighbours,selectedMovie);
        }

        let recommendations: Movie[] = [];

        for (let movie of randomMovies) {
            if (movie !== undefined) {
                let explanation = await getNeighbourExplanation(movie.parent, movie.child);
                console.log("explanation");
                console.log(explanation);
                console.log("\n")
                // recommendations.concat(await getNeighbourExplanation(movie))
            }
        }

        console.log(recommendations);

        // if( this.isAllRatingsSubmitted()){
        //     const liked = this.getMoviesOverLikeThreshold();

        //     for( const movie of liked) {
        //         this.topRecommendation = this.topRecommendation.concat(movie);
        //         this.topRecommendation = this.topRecommendation.concat(await getSuggestions(movie.title));
        //         //console.log('liked', movie, 'recommended', this.topRecommendation);
        //     }
        //     notifyProcessingDone(this.members, this.topRecommendation);
        // }

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
