import random
import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import linear_kernel
import re
from models import Movie

# from sklearn.neighbors import NearestNeighbors
# from lime.lime_text import LimeTextExplainer

pd.set_option('display.max_colwidth', None)
df = pd.read_csv("new_knn.csv")

tfidf = TfidfVectorizer(stop_words="english")
tfidf_matrix = tfidf.fit_transform(df['soup'])
cosine_sim = linear_kernel(tfidf_matrix, tfidf_matrix)

# model = NearestNeighbors(metric='cosine')
# model.fit(tfidf_matrix)

def get_random_movies(number_of_movies, top=100):
    top_movies = df.sort_values(by=['vote_count'], ascending=False)[:top]
    random_movie_indices = random.sample(range(len(top_movies)), number_of_movies)
    random_movies = top_movies.iloc[random_movie_indices]
    return random_movies

def get_similar_movies(movie_title, k=3):
    movie_index = df[df['title'] == movie_title].index[0]
    sim_scores = list(enumerate(cosine_sim[movie_index]))
    sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
    sim_scores = sim_scores[1:k+1]
    movie_indices = [i[0] for i in sim_scores]
    return df['title'].iloc[movie_indices]

def get_neighbour_explanation(parent,child):
    parent_soup = df.loc[df['title'] == parent.title, 'soup'].iloc[0]
    child_soup = df.loc[df['title'] == child.title, 'soup'].iloc[0]
    parent_vector = tfidf.transform([parent_soup])
    child_vector = tfidf.transform([child_soup])
    parent_words = {word: val for word, val in zip(tfidf.get_feature_names_out(), parent_vector.toarray()[0]) if val > 0}
    child_words = {word: val for word, val in zip(tfidf.get_feature_names_out(), child_vector.toarray()[0]) if val > 0}
    common_words = set(parent_words.keys()) & set(child_words.keys())
    explanation = {word: (parent_words[word], child_words[word]) for word in common_words}
    sorted_words = sorted(explanation.items(), key=lambda x: (x[1][0] + x[1][1]) / 2, reverse=True)
    top_10_words = {word: sum(values) for word, values in sorted_words[:10]}
    processed = process_top(top_10_words, child_soup)
    
    childMovie = df.loc[df['title'] == child.title].iloc[0]
    movie = Movie(
        index=childMovie['index'],
        title=child.title,
        overview=childMovie['overview'],
        full_poster_path=childMovie['poster_path'],
        explanation=str(processed)
    )
    
    return movie

def process_top(top, soup):
    actors = soup.split()[-6:]
    actors_lower = [name.lower() for name in actors]
    split_actors = [re.sub(r'([a-z])([A-Z])', r'\1 \2', name) for name in actors]

    proccessed = {}

    for key,val in top.items():
        if key in actors_lower:
            index = actors_lower.index(key)
            proccessed[split_actors[index]] = val
        else:
            proccessed[key.title()] = val
    return proccessed