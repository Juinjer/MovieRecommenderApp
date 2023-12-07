from fastapi import FastAPI
from movie_recommender_model import get_similar_movies, get_explanation
from models import Movie

app = FastAPI()


@app.get("/")
async def read_root():
    return {"Hello": "World"}


@app.post("/submitFilms/{filmids}")
async def submit_films(filmids: str):
    # get some recommendation from model
    recommendation = get_recommendation(filmids)

    return recommendation


# TODO: Decide whether films get passed with their title or their index
@app.get("/simple_recommendation")
async def get_recommendation(movie: Movie):
    similar_movies = get_similar_movies(movie.title)
    recommendations = [
        {"index": index, "title": title, "overview": overview}
        for index, (title, overview) in similar_movies[['title', 'overview']].iterrows()
    ]
    return {"movie_title": movie.title, "recommendations": recommendations}


# The explanation is the intensive part
@app.get("/full_recommendation")
async def get_recommendation(movie: Movie):
    similar_movies = get_similar_movies(movie.title)
    explanations = get_explanation(similar_movies)
    recommendations = []

    for index, (title, overview), explanation in zip(similar_movies.index,
                                                     similar_movies[['title', 'overview']].itertuples(index=False,
                                                                                                      name=None),
                                                     explanations):
        recommendations.append({
            "index": index,
            "title": title,
            "overview": overview,
            "explanation": explanation
        })
    return {"movie_title": movie.title, "recommendations": recommendations}
