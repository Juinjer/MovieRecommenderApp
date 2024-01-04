from pydantic import BaseModel

class Movie(BaseModel):
    index: int
    title: str
    overview: str
    full_poster_path: str
    explanation: str

class MovieTitle(BaseModel):
    title: str