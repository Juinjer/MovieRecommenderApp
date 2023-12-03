from fastapi import FastAPI

# from typing import Union
from examplemodel import get_recommendation

app = FastAPI()



@app.get("/")
def read_root():
    return {"Hello":"World"}


@app.post("/submitFilms/{filmids}")
def submit_films(filmids:str):
    # get some recommendation from model
    recom = get_recommendation(filmids)

    return recom