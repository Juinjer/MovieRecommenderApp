import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.neighbors import NearestNeighbors
import lime
from lime.lime_text import LimeTextExplainer

pd.set_option('display.max_colwidth', None)
df = pd.read_csv("knn_data.csv")

tfidf = TfidfVectorizer(stop_words="english")
tfidf_matrix = tfidf.fit_transform(df['overview'])
print(tfidf_matrix.shape)

model = NearestNeighbors(metric='cosine')
model.fit(tfidf_matrix)


def get_similar_movies(movie_title, k=5):
    movie_index = df[df['title'] == movie_title].index[0]
    distances, indices = model.kneighbors(tfidf_matrix[movie_index],
                                          n_neighbors=k + 1)
    similar_movies = df.iloc[indices.flatten()[1:]]
    return similar_movies


def get_explanation(similar_movies):
    explainer = lime.lime_text.LimeTextExplainer(class_names=['Similar', 'Not Similar'])

    explanations = []  # List to store explanations

    for i in range(len(similar_movies)):
        print(f'Similar movie #{i + 1}: {similar_movies["title"].iloc[i]}')
        movie_overview = similar_movies['overview'].iloc[i]
        explanation = explainer.explain_instance(
            movie_overview, predict_similarity, num_features=10)
        # explanation.show_in_notebook()

        # Format the explanation details as a string
        exp_str = "{" + ", ".join(f'"{word}": {weight}' for word, weight in explanation.as_list()) + "}"

        explanations.append(exp_str)

    return explanations


def predict_similarity(texts):
    text_vectors = tfidf.transform(texts)
    distances, indies = model.kneighbors(text_vectors, n_neighbors=5)
    return distances[:, 1:]
