import logging
from fastapi import FastAPI
import movie_recommender_model as model
from models import MovieId
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
            "id": movie_id,
            "title": title,
            "overview": overview,
            "full_poster_path": base_poster_url + poster_path
        }
        for index, (movie_id, title, overview, poster_path) in random_movies[['id', 'title', 'overview', 'poster_path']].iterrows()
    ]
    #    logging.info({"movie_title": movie.title, "recommendations": recommendations})
    return {"movies": suggestions}


@app.post("/3nn")
async def get_3nn(movie_id: MovieId):
    similar_movies = model.get_similar_movies(movie_id.id, 3)
    # print(similar_movies);
    recommendations = similar_movies.id.tolist()

    # [
    #     {
    #         "index": index,
    #         "title": title,
    #         "overview": overview,
    #         "full_poster_path": base_poster_url + poster_path
    #     }
    #     for index, (title, overview, poster_path) in similar_movies[['title', 'overview', 'poster_path']].iterrows()
    # ]

    return {"movie_id": movie_id.id, "recommendations": recommendations}


@app.post("/neighbour_explanation")
async def get_neighbour_explanation(parent: MovieId, child: MovieId):
    try:
        movie_info = model.get_movie_info_by_id(child.id)
    except Exception as e:
        return {"error": f"Error: {str(e)}", "status_code": 404}

    child_movie = {
        "id": movie_info['id'].item(),
        "title": movie_info['title'].item(),
        "overview": movie_info['overview'].item(),
        "full_poster_path": base_poster_url + movie_info['poster_path'].item()
    }

    explanation = model.get_neighbour_explanation(parent.id, child.id)
    child_movie['explanation'] = json.dumps(explanation)
    return {"recommendation": child_movie}
