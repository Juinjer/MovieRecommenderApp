import random
import lime
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.neighbors import NearestNeighbors
from lime.lime_text import LimeTextExplainer

pd.set_option('display.max_colwidth', None)
df = pd.read_csv("new_knn.csv")

tfidf = TfidfVectorizer(stop_words="english")
tfidf_matrix = tfidf.fit_transform(df['soup'])
print(tfidf_matrix.shape)

model = NearestNeighbors(metric='cosine')
model.fit(tfidf_matrix)


def get_random_movies(number_of_movies, top=100):
    top_movies = df.sort_values(by=['vote_count'], ascending=False)[:top]
    random_movie_indices = random.sample(range(len(top_movies)), number_of_movies)
    random_movies = top_movies.iloc[random_movie_indices]

    return random_movies


def get_similar_movies(movie_title, k=3):
    print("similar movies")
    movie_index = df[df['title'] == movie_title].index[0]
    distances, indices = model.kneighbors(tfidf_matrix[movie_index],
                                          n_neighbors=k + 1)
    similar_movies = df.iloc[indices.flatten()[1:]]
    # similar_movies = df.iloc[indices.flatten()]
    return similar_movies

def get_neighbour_explanation(parent, child):
    parent_soup = df.loc[parent.index, 'soup']
    child_soup = df.loc[child.index, 'soup']
    print(parent_soup)
    # explainer = LimeTextExplainer(class_names=['Similar', 'Not Similar'])
    # explanations = []
    # explanation = explainer.explain_instance(child.overview, lambda text: predict_similarity(parent.overview, text), num_features=10)
    # exp_str = "{" + ", ".join(f'"{word}": {weight}' for word, weight in explanation.as_list()) + "}"
    # explanations.append(exp_str)
    # return explanations
    
# def predict_similarity(parent_text,child_text):
    # parent_vector = tfidf.transform([parent_text])
    # child_vector = tfidf.transform([child_text])
    # distances, indies = model.kneighbors(parent_vector, n_neighbors=2)
    # similarity_score = distances[0, 1]
    # return similarity_score

# def get_explanation(similar_movies):
#     print("explaination")
#     explainer = lime.lime_text.LimeTextExplainer(class_names=['Similar', 'Not Similar'])

#     explanations = []  # List to store explanations

#     for i in range(len(similar_movies)):
#         print(f'Similar movie #{i + 1}: {similar_movies["title"].iloc[i]}')
#         movie_overview = similar_movies['soup'].iloc[i]
#         explanation = explainer.explain_instance(
#             movie_overview, predict_similarity, num_features=10)
#         # explanation.show_in_notebook()

#         # Format the explanation details as a string
#         exp_str = "{" + ", ".join(f'"{word}": {weight}' for word, weight in explanation.as_list()) + "}"

#         explanations.append(exp_str)

#     return explanations

# # def predict_similarity(texts):
# #     text_vectors = tfidf.transform(texts)
# #     distances, indies = model.kneighbors(text_vectors, n_neighbors=3)
# #     return distances[:, 1:]
