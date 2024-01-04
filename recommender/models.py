from pydantic import BaseModel


class Movie(BaseModel):
    id: int
    title: str
    overview: str
    full_poster_path: str
    explanation: str


class MovieId(BaseModel):
    id: int
