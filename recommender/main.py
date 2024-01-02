import logging
from fastapi import FastAPI
import movie_recommender_model as model
from models import Movie, MovieTitle

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


# # TODO: Decide whether films get passed with their title or their index
# @app.post("/simple_recommendation")
# async def get_simple_recommendation(movie: Movie):
#     similar_movies = model.get_similar_movies(movie.title)
#     recommendations = [
#         {
#             "index": index,
#             "title": title,
#             "overview": overview,
#             "full_poster_path": base_poster_url + poster_path
#         }
#         for index, (title, overview, poster_path) in similar_movies[['title', 'overview', 'poster_path']].iterrows()
#     ]
#     #    logging.info({"movie_title": movie.title, "recommendations": recommendations})
#     return {"movie_title": movie.title, "recommendations": recommendations}

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
    #    logging.info({"movie_title": movie.title, "recommendations": recommendations})
    return {"movie_title": movie.title, "recommendations": recommendations}


@app.post("/neighbour_explanation")
async def get_neighbour_explanation(parent: Movie, child: Movie):
    explanations = model.get_neighbour_explanation(parent, child)
    # print("explanations")
    # print(explanations)
    # print("\n")


# # The explanation is the intensive part
# @app.post("/full_recommendation")
# async def get_full_recommendation(movie: Movie):
#     similar_movies = model.get_similar_movies(movie.title)
#     explanations = model.get_explanation(similar_movies)
#     recommendations = []

#     for index, (title, overview, poster_path), explanation in zip(similar_movies.index,
#                                                                   similar_movies[
#                                                                       ['title', 'overview', 'poster_path']].itertuples(
#                                                                       index=False,
#                                                                       name=None),
#                                                                   explanations):
#         recommendations.append({
#             "index": index,
#             "title": title,
#             "overview": overview,
#             "full_poster_path": base_poster_url + poster_path,
#             "explanation": explanation
#         })
#     return {"movie_title": movie.title, "recommendations": recommendations}

# @app.post("/send_likes")
# async def add_likes( like: UserLike):