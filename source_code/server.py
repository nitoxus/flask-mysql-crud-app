'''
Created on Jan 10, 2017

@author: hanif
'''

from flask import Flask, flash, render_template, redirect, url_for, request, session
from module.database import Database
from prometheus_flask_exporter import PrometheusMetrics
from logging.config import dictConfig

dictConfig({
    'version': 1,
    'formatters': {'default': {
        'format': '[%(asctime)s] %(levelname)s in %(module)s: %(message)s',
    }},
    'handlers': {'wsgi': {
        'class': 'logging.StreamHandler',
        'stream': 'ext://flask.logging.wsgi_errors_stream',
        'formatter': 'default'
    }},
    'root': {
        'level': 'DEBUG',
        'handlers': ['wsgi']
    }
})

app = Flask(__name__)
app.secret_key = "mys3cr3tk3y"
db = Database()

metrics = PrometheusMetrics(app)
metrics.info('app_info', 'Application info', version='1.0.3')

@app.route('/')
def index():
    data = db.read(None)
    
    return render_template('index.html', data=data)


@app.route('/add/')
def add():
    app.logger.info("Rendering add.html.")
    return render_template('add.html')


@app.route('/addphone', methods=['POST', 'GET'])
def addphone():
    if request.method == 'POST' and request.form['save']:
        if db.insert(request.form):
            app.logger.info("A new phone number has been added")
            flash("A new phone number has been added")
        else:
            app.logger.error("A new phone number can not be added")
            flash("A new phone number can not be added")

        return redirect(url_for('index'))
    else:
        return redirect(url_for('index'))


@app.route('/update/<int:id>/')
def update(id):
    data = db.read(id)
    app.logger.info("Update id: {}".format(id))
    if len(data) == 0:
        app.logger.error("Nothing to update.")
        return redirect(url_for('index'))
    else:
        session['update'] = id
        return render_template('update.html', data=data)


@app.route('/updatephone', methods=['POST'])
def updatephone():
    if request.method == 'POST' and request.form['update']:

        if db.update(session['update'], request.form):
            app.logger.info('A phone number has been updated')
            flash('A phone number has been updated')
        else:
            app.logger.error('A phone number can not be updated')
            flash('A phone number can not be updated')

        session.pop('update', None)

        return redirect(url_for('index'))
    else:
        return redirect(url_for('index'))


@app.route('/delete/<int:id>/')
def delete(id):
    data = db.read(id)
    app.logger.info("Delete id: {}".format(id))
    if len(data) == 0:
        app.logger.error("Nothing to delete.")
        return redirect(url_for('index'))
    else:
        session['delete'] = id
        return render_template('delete.html', data=data)


@app.route('/deletephone', methods=['POST'])
def deletephone():
    if request.method == 'POST' and request.form['delete']:
        
        if db.delete(session['delete']):
            app.logger.info('A phone number has been deleted')
            flash('A phone number has been deleted')

        else:
            app.logger.error('A phone number can not be deleted')
            flash('A phone number can not be deleted')

        session.pop('delete', None)

        return redirect(url_for('index'))
    else:
        return redirect(url_for('index'))


@app.errorhandler(404)
def page_not_found(error):
    app.logger.error('The page not found.')
    return render_template('error.html')


if __name__ == '__main__':
    app.logger.info('Starting app.')
    app.run(port=8181, host="0.0.0.0")
