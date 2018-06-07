#!/bin/env python3

import csv
import mysql.connector

CATEGORIES = [
    'animals',
    'history',
    'geography',
    'music',
    'sports',
]
DB_CONFIG = {
    'host': 'localhost',
    'database': 'lis',
    'user': 'lis',
    'password': 'lis',
    'use_unicode':'True',
    'charset':'utf8mb4'
}
DB_TABLES = ['questions', 'answers', 'categories']


def clean_database():
    print("[INFO] Cleaning database")
    con = mysql.connector.connect(**DB_CONFIG)
    cursor = con.cursor()
    for table in DB_TABLES:
        cursor.execute("SET FOREIGN_KEY_CHECKS = 0")
        cursor.execute("TRUNCATE TABLE {}".format(table))
        cursor.execute("SET FOREIGN_KEY_CHECKS = 1")
    con.commit()
    cursor.close()
    con.close()


def load():
    clean_database()

    con = mysql.connector.connect(**DB_CONFIG)
    cursor = con.cursor(named_tuple=True)

    for category in CATEGORIES:
        print("[INFO] Inserting category '{}'".format(category))
        cursor.execute("INSERT INTO categories SET name = %s",
                       (category.title(), ))
        category_id = cursor.lastrowid

        filename = 'Question_set_csv/{}_question_set.csv'.format(category)
        with open(filename, 'r', encoding="utf-8") as csvfile:
            reader = csv.reader(csvfile, quotechar='"', skipinitialspace=True)
            for row in reader:
                difficulty = int(row[0])
                question = row[1].strip()
                answers = [s.strip() for s in row[2:6]]

                # insert the question
                cursor.execute("SET names latin1;",)
                cursor.execute(
                    "INSERT INTO questions (userId, categoryId, `text`, difficulty)"
                    " VALUES (%s, %s, %s, %s)",
                    (None, category_id, question, difficulty))
                question_id = cursor.lastrowid

                # insert the answers
                cursor.execute("SET names latin1;",)
                for (index, answer) in enumerate(answers):
                    assert len(answer) > 0, "[{}] {}".format(
                        category, question)
                    cursor.execute(
                        "INSERT INTO answers (questionId, `text`, isCorrect)"
                        " VALUES (%s, %s, %s)",
                        (question_id, answer, index == 0))

    con.commit()
    cursor.close()
    con.close()


if __name__ == '__main__':
    load()
