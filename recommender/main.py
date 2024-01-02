import logging
from fastapi import FastAPI
import movie_recommender_model as model
from models import Movie, MovieTitle
import json

# Add the base URL for poster images
base_poster_url = 'https://image.tmdb.org/t/p/original/'

app = FastAPI()


@app.get("/")
async def read_root():
    return {"Hello": "World"}


@app.get("/random/{number_of_movies}")
async def get_random_movies(number_of_movies: int):
    random_movies = model.get_random_movies(number_of_movies)

    suggestions = [
        {
            "index": index,
            "title": title,
            "overview": overview,
            "full_poster_path": base_poster_url + poster_path
        }
        for index, (title, overview, poster_path) in random_movies[['title', 'overview', 'poster_path']].iterrows()
    ]
    #    logging.info({"movie_title": movie.title, "recommendations": recommendations})
    return {"movies": suggestions}

@app.post("/3nn")
async def get_3nn(movie: MovieTitle):
    similar_movies = model.get_similar_movies(movie.title,3)
    print(similar_movies);
    recommendations = [
        {
            "index": index,
            "title": title,
            "overview": overview,
            "full_poster_path": base_poster_url + poster_path
        }
        for index, (title, overview, poster_path) in similar_movies[['title', 'overview', 'poster_path']].iterrows()
    ]
    return {"movie_title": movie.title, "recommendations": recommendations}


@app.post("/neighbour_explanation")
async def get_neighbour_explanation(parent: Movie, child: Movie):
    explanation = model.get_neighbour_explanation(parent, child)
    child.explanation = json.dumps(explanation)
    return { "recommendation": child }
