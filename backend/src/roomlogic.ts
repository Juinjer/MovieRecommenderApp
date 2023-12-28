import { MovieRating, Movie} from './interfaces'
import { getSuggestions, getSuggestionsRandom} from './api';
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
        //console.log(movie, rating);
        let ratings: MovieRating[] = this.movieRatings.get(movie) || [];
        ratings.push(rating); // This is safe because ratings is guaranteed to be an array
        this.movieRatings.set(movie, ratings);

        if( this.isAllRatingsSubmitted()){
            const liked = this.getMoviesOverLikeThreshold();

            for( const movie of liked) {
                this.topRecommendation = this.topRecommendation.concat(movie);
                this.topRecommendation = this.topRecommendation.concat(await getSuggestions(movie.title));
                //console.log('liked', movie, 'recommended', this.topRecommendation);
            }
            notifyProcessingDone(this.members, this.topRecommendation);
        }
    }

    getMoviesOverLikeThreshold(): Movie[]{
        const moviesOverThreshold: Movie[] = []

        for(const movie of this.movieRatings.keys()){
            let positive:number = 0;
            const ratings: MovieRating[] = this.movieRatings.get(movie)!!;

            for(const rating of ratings){
                if(rating.rating == 1){ positive += 1;}
            }

            const likePercentage = positive/ratings.length
            if( likePercentage> this.likeThreshold){
                movie.explanation = `${likePercentage*100}% of you liked this movie`

                // Do not iterate over title strings, instead connect every like to the Movie object in the map
                moviesOverThreshold.push(movie);
            }
        }
        return moviesOverThreshold;
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
