#!/usr/bin/env python

from argparse import ArgumentParser
import os
from os import makedirs
from os.path import splitext, exists
from pickle import load
import pandas as pd
import numpy as np
import re
from keras.models import load_model


def word2_vec_model(trimmed_dict):
    def word_2_id(word):
        if word in trimmed_dict:
            return np.array(trimmed_dict[word])
        return None

    def text_2_id(text):
        regex = re.compile('[^a-zA-Z ]')
        words = re.sub(regex, '', text).lower().split()
        res = np.zeros((0, 1), dtype='int32')
        for word in words:
            vec = word_2_id(word)
            if vec is not None:
                res = np.vstack((res, vec))
        return res

    return text_2_id


def calc_sentiment(Keras_model, text_2_id, text):
    input_len = Keras_model.layers[0].input_shape[1] # 200
    vec_id = text_2_id(text)
    if len(vec_id) < input_len:
        vec_id_tmp = np.zeros((1, input_len))
        vec_id_tmp[:,:len(vec_id)] = np.array(vec_id).reshape((1, len(vec_id)))
        vec_id = vec_id_tmp
    begin_i = 0
    end_i = min(len(vec_id), input_len)
    sentiment_res = []
    while end_i <= len(vec_id):
        text_df = np.array(vec_id[begin_i:end_i]).reshape((1, input_len))
        sent = Keras_model.predict(text_df, batch_size=1)[0][0]
        sentiment_res += [sent]
        if end_i == len(vec_id):
            break
        end_i = min(end_i + input_len, len(vec_id))
        begin_i = end_i - input_len
    if len(sentiment_res) > 1:
        sentiment_res[-2] = (sentiment_res[-2] + sentiment_res[-1]) / 2
        del sentiment_res[-1]
    return np.array(sentiment_res).mean()


def process_file(file_name, output_path, word2_vec_model, sentiment_model):
    with open(file_name, 'r') as f:
        text = " ".join(f.readlines())
        res = calc_sentiment(sentiment_model, word2_vec_model, text)
    print(res)
    with open(output_path, 'w+') as f:
        f.write(str(res))


def main():
    parser = ArgumentParser(description='Sentiment analyser')
    input_group = parser.add_mutually_exclusive_group(required=True)
    input_group.add_argument('--input_directory', type=str, nargs='?', default=None,
                             help='path to directory with *.txt texts')
    input_group.add_argument('--input_file', type=str, nargs='?', default=None,
                             help='path to *.txt file with texts')

    output_group = parser.add_mutually_exclusive_group(required=True)
    output_group.add_argument('--output_directory', type=str, nargs='?', default=None,
                              help='path to directory to write texts with sentiment marks')
    output_group.add_argument('--output_file', type=str, nargs='?', default=None,
                              help='path to file to write text with sentiment mark')
    parser.add_argument('--word2vec_model', type=str, nargs='?', default='sentiment_model.pkl',
                        help='sentiment model')
    parser.add_argument('--sentiment_model', type=str, nargs='?', default='sentiment_model_keras.pkl',
                        help='sentiment model')
    args = parser.parse_args()

    if args.input_file is None:
        files = []
        for root, dirs, walk_files in os.walk(args.input_directory):
            for f in walk_files:
                if f.endswith('.csv'):
                    files.append(os.path.join(root, f))
    else:
        files = [args.input_file]

    if args.output_file is None:
        output_paths = []
        for root, dirs, walk_files in os.walk(args.input_directory):
            for f in walk_files:
                if f.endswith('.csv'):
                    if not exists(os.path.join(args.output_directory, root)):
                        makedirs(os.path.join(args.output_directory, root))
                    filename = splitext(f)[0]
                    new_name = filename + '_marked.csv'
                    output_paths.append(os.path.join(args.output_directory, root, new_name))
    else:
        output_paths = [args.output_file]

    with open(args.word2vec_model, 'r') as f:
        trimmed_dict = load(f)

    sentiment_model = load_model(args.sentiment_model)

    for file_name, output_path in zip(files, output_paths):
        process_file(file_name, output_path, word2_vec_model(trimmed_dict), sentiment_model)

if __name__ == "__main__":
    main()